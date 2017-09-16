package com.example.ligh.camera.encoder;

/**
 * Created by ligh on 2017/9/10.
 */

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.example.ligh.camera.drawer.OESTextureDrawer;
import com.example.ligh.camera.programs.OESTextureShaderProgram;
import com.example.ligh.camera.utils.GLog;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 视频编码器
 */
public class VideoEncoder implements Handler.Callback
{
    private static final String  MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;

    private String OUTPUT_DIR  = Environment.getExternalStorageDirectory().getPath();

    private static final int PREPARE_MESSAGE = 1;
    private static final int START_MESSAGE = 2;
    private static final int STOP_MESSAGE = 3;
    private static final int DRAIN_MESSAGE = 4;
    private static final int RELEASE_MESSAGE = 5;


    //定义一个16x16的透视矩阵
    private float[] mProjectMatrix = new float[16];

    // 编码器线程
    private HandlerThread mHanderThread;
    private Handler mHandler;

    private EGLContext mEGLDisplayContext;
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;

    private CodecInputSurface mInputSurface;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    private int mWidth = 320;
    private int mHeight = 480;

    private OESTextureShaderProgram mSharedProgram;
    private OESTextureDrawer mDrawer;
    private int mTextureId;

    private Context mContext;


    public VideoEncoder(Context context, int textureId)
    {
        this.mTextureId = textureId;

        this.mContext = context;

        mHanderThread = new HandlerThread("VideoEncoderThread");
        mHanderThread.start();

        mHandler = new Handler(mHanderThread.getLooper(),this);
    }

    public void setOutputSize(int width,int height)
    {
        this.mWidth = width;
        this.mHeight = height;
    }

    /**
     * 准备编码器
     */
    public void prepareEncoder()
    {
        mHandler.sendEmptyMessage(PREPARE_MESSAGE);
    }

    private void _prepareEncoder()
    {
        if (mEncoder != null) return;

        mBufferInfo = new MediaCodec.BufferInfo();

//        mSurface = new Surface(mSurfaceTexture);

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        GLog.i("format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.

        try
        {

            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());

        } catch (Exception e)
        {
            mEncoder = null;
        }

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        String outputPath = new File(OUTPUT_DIR,
                "test." +System.currentTimeMillis()+ ".mp4").toString();
        GLog.i("output file is " + outputPath);


        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * 启动录制
     */
    public void startRecording(EGLContext eglContext)
    {
        if (eglContext == null)
        {
            GLog.e("startRecording : The eglContext parameter cannot be null");
            return;
        }
        mSharedProgram = new OESTextureShaderProgram(this.mContext);
        mDrawer = new OESTextureDrawer();
        this.mEGLDisplayContext = eglContext;

        mHandler.sendEmptyMessage(START_MESSAGE);
    }

    private void _startRecording()
    {
        _prepareEncoder();

        if (mEncoder == null) return;




        mInputSurface.makeCurrent();
        mEncoder.start();
    }

    public float[] getProjectMatrix()
    {
        return mProjectMatrix;
    }

    public void drainEncoder(boolean endOfStream)
    {
        mHandler.sendMessage(mHandler.obtainMessage(DRAIN_MESSAGE,endOfStream));
    }

    private void _drawFrame()
    {

        mSharedProgram.useProgram();
        mSharedProgram.setUniforms(mProjectMatrix,mTextureId);
        mDrawer.bindData(mSharedProgram);
        mDrawer.draw();


        mInputSurface.setPresentationTime( System.nanoTime());

        // Submit it to the encoder.  The eglSwapBuffers call will block if the input
        // is full, which would be bad if it stayed full until we dequeued an output
        // buffer (which we can't do, since we're stuck here).  So long as we fully drain
        // the encoder before supplying additional input, the system guarantees that we
        // can supply another frame without blocking.
        GLog.i("sending frame to encoder");
        mInputSurface.swapBuffers();
    }

    private void _drainEncoder(boolean endOfStream)
    {
        final int TIMEOUT_USEC = 10000;

        GLog.d("drainEncoder(" + endOfStream + ")");

        if (endOfStream)
        {
            GLog.d("sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    GLog.d("no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                GLog.d( "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                GLog.d("unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    GLog.d("ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    GLog.d("writeSampleData(" + endOfStream + ")");

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    GLog.i("sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        GLog.d("reached end of stream unexpectedly");
                    } else {
                        GLog.d("end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording()
    {
        mHandler.sendEmptyMessage(STOP_MESSAGE);
    }

    /**
     * 停止录制
     */
    private void _stopRecording()
    {
        if (mEncoder == null) return;
    }

    public void releaseEncoder()
    {
        stopRecording();
        mHandler.sendEmptyMessage(RELEASE_MESSAGE);
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private void _releaseEncoder()
    {
        GLog.i( "releasing encoder objects");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    /**
     * 处理消息
     * @param message
     * @return
     */
    @Override
    public boolean handleMessage(Message message)
    {
        switch (message.what)
        {
            case PREPARE_MESSAGE :
                _prepareEncoder();
                break;
            case START_MESSAGE :
                _startRecording();
                break;
            case STOP_MESSAGE :
                _stopRecording();
                break;
            case DRAIN_MESSAGE :
                _drawFrame();
                _drainEncoder((boolean)message.obj);
                break;
            case RELEASE_MESSAGE:
                _releaseEncoder();
                break;
        }

        return false;
    }

    /**
     * Holds state associated with a Surface used for MediaCodec encoder input.
     * <p>
     * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses that
     * to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to be sent
     * to the video encoder.
     * <p>
     * This object owns the Surface -- releasing this will release the Surface too.
     */
    private  class CodecInputSurface {
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

        private Surface mSurface;

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        public CodecInputSurface(Surface surface) {
            if (surface == null) {
                throw new NullPointerException();
            }
            mSurface = surface;

            eglSetup();
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for recording and OpenGL ES 2.0.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_NONE
            };


            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0);
            checkEglError("eglCreateContext RGB888+recordable ES2");

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };


            mEGLContext =  EGL14.eglCreateContext(mEGLDisplay, configs[0],mEGLDisplayContext,
                    attrib_list, 0);
            checkEglError("eglCreateContext");

            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface,
                    surfaceAttribs, 0);
            checkEglError("eglCreateWindowSurface");
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  Also releases the
         * Surface that was passed to our constructor.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }

            mSurface.release();

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            checkEglError("eglMakeCurrent");
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         */
        public boolean swapBuffers() {
            boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            checkEglError("eglSwapBuffers");
            return result;
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         */
        public void setPresentationTime(long nsecs) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
            checkEglError("eglPresentationTimeANDROID");
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }
}
