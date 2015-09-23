package org.zywx.wbpalmstar.plugin.uexunisound;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import cn.yunzhisheng.common.USCError;
import cn.yunzhisheng.nlu.basic.USCSpeechUnderstander;
import cn.yunzhisheng.nlu.basic.USCSpeechUnderstanderListener;
import cn.yunzhisheng.tts.online.basic.OnlineTTS;
import cn.yunzhisheng.tts.online.basic.TTSPlayerListener;
import cn.yunzhisheng.understander.USCUnderstanderResult;

public class EUExUnisound extends EUExBase {
    private static final String TAG = "EUExUnisound";
    private static final String BUNDLE_DATA = "data";

    private static final int MSG_INIT = 1;
    private static final int MSG_UPDATE_RECOGNIZER_SETTINGS = 2;
    private static final int MSG_START = 3;
    private static final int MSG_STOP = 4;
    private static final int MSG_CANCEL = 5;
    private static final int MSG_TEXT_UNDERSTANDER = 6;
    private static final int MSG_SPEAKING = 7;
    private static final int MSG_CANCEL_SPEAKING = 8;

    private USCSpeechUnderstander mUscSpeechUnderstander;
    private OnlineTTS mOnlineTTS;

    public EUExUnisound(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }

    public void init(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_INIT;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void initMsg(String[] params) {
        String json = params[0];
        String appKey = null;
        String secret = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            appKey=jsonObject.optString("appKey");
            secret=jsonObject.optString("secret");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mUscSpeechUnderstander = new USCSpeechUnderstander(mContext.getApplicationContext(),appKey,secret);
        mUscSpeechUnderstander.setListener(new USCSpeechUnderstanderListener() {

            @Override
            public void onSpeechStart() {
                callBackPluginJs(JsConst.ON_SPEECH_START,"");
            }

            @Override
            public void onRecordingStart() {
//                callBackPluginJs(JsConst.ON_RECORDING_START, "");
            }

            @Override
            public void onRecordingStop() {
//                callBackPluginJs(JsConst.ON_RECORDING_STOP,"");
            }

            @Override
            public void onVADTimeout() {
                callBackPluginJs(JsConst.ON_V_A_D_TIMEOUT,"");
            }

            @Override
            public void onRecognizerResult(String result, boolean isLast) {
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("result",result);
                    jsonObject.put("isLast",isLast);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBackPluginJs(JsConst.ON_RECEIVE_RECOGNIZER_RESULT, jsonObject.toString());
            }

            @Override
            public void onUpdateVolume(int i) {
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("volume",i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBackPluginJs(JsConst.ON_UPDATE_VOLUME,jsonObject.toString());
            }

            @Override
            public void onUnderstanderResult(USCUnderstanderResult uscUnderstanderResult) {
                callBackPluginJs(JsConst.ON_RECEIVE_UNDERSTANDER_RESULT,uscUnderstanderResult.getStringResult());
            }

            @Override
            public void onEnd(USCError uscError) {
                JSONObject jsonObject = new JSONObject();
                try {
                    if (uscError==null){
                        jsonObject.put("result", 0);
                    }else {
                        jsonObject.put("result", uscError.code);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBackPluginJs(JsConst.ON_END,jsonObject.toString());
            }
        });
        mUscSpeechUnderstander.start();
        mOnlineTTS = new OnlineTTS(mContext.getApplicationContext(),appKey);
        mOnlineTTS.setTTSListener(new TTSPlayerListener() {
            @Override
            public void onPlayBegin() {
                callBackPluginJs(JsConst.ON_SPEAKING_START, "");
            }

            @Override
            public void onBuffer() {
            }

            @Override
            public void onEnd(USCError uscError) {
                if (uscError != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("error", uscError.msg);
                    } catch (JSONException e) {
                        Log.i(TAG, "------onEnd:" + e.getMessage());
                    }
                    callBackPluginJs(JsConst.ON_SPEAKING_ERROR_OCCURRED, jsonObject.toString());
                }
            }

            @Override
            public void onPlayEnd() {
                callBackPluginJs(JsConst.ON_SPEAKING_FINISHED, "");
            }
        });
    }

    public void updateRecognizerSettings(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_UPDATE_RECOGNIZER_SETTINGS;
        mHandler.sendMessage(msg);
    }

    //和ios的区别在于不能设置recognizationTimeout，needUnderstander
    private void updateRecognizerSettingsMsg(String[] params) {
        String json = params[0];
        try {
            JSONObject jsonObject = new JSONObject(json);
            int frontTime = jsonObject.optInt("frontTime", 3000);
            int backTime = jsonObject.optInt("backTime", 1000);
            int rate = jsonObject.optInt("rate", 3);
            int engine = jsonObject.optInt("engine", 1);
            int language = jsonObject.optInt("language", 1);
            switch (engine) {
                case 1: mUscSpeechUnderstander.setEngine("general");break;
                case 2: mUscSpeechUnderstander.setEngine("poi"); break;
                case 3: mUscSpeechUnderstander.setEngine("song"); break;
                case 4: mUscSpeechUnderstander.setEngine("movietv"); break;
                case 5: mUscSpeechUnderstander.setEngine("medical"); break;
                default: mUscSpeechUnderstander.setEngine("general");break;
            }
            switch (rate) {
                case 1: mUscSpeechUnderstander.setBandwidth(USCSpeechUnderstander.BANDWIDTH_AUTO); break;
                case 2: mUscSpeechUnderstander.setBandwidth(USCSpeechUnderstander.RATE_8K); break;
                case 3: mUscSpeechUnderstander.setBandwidth(USCSpeechUnderstander.RATE_16K); break;
                default: mUscSpeechUnderstander.setBandwidth(USCSpeechUnderstander.BANDWIDTH_AUTO); break;
            }
            switch (language) {
                case 1: mUscSpeechUnderstander.setLanguage(USCSpeechUnderstander.LANGUAGE_CHINESE); break;
                case 2: mUscSpeechUnderstander.setLanguage(USCSpeechUnderstander.LANGUAGE_ENGLISH); break;
                case 3: mUscSpeechUnderstander.setLanguage(USCSpeechUnderstander.LANGUAGE_CANTONESE); break;
                default: mUscSpeechUnderstander.setLanguage(USCSpeechUnderstander.LANGUAGE_CHINESE); break;
            }
            mUscSpeechUnderstander.setVADTimeout(frontTime, backTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void start(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_START;
        mHandler.sendMessage(msg);
    }

    private void startMsg(String[] params) {
        mUscSpeechUnderstander.start();
    }

    public void stop(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_STOP;
        mHandler.sendMessage(msg);
    }

    private void stopMsg(String[] params) {
        mUscSpeechUnderstander.stop();
    }

    public void cancel(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_CANCEL;
        mHandler.sendMessage(msg);
    }

    private void cancelMsg(String[] params) {
        mUscSpeechUnderstander.cancel();
    }

    public void runTextUnderstand(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_TEXT_UNDERSTANDER;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void runTextUnderstandMsg(String[] params) {
        String json = params[0];
        String text=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            text = jsonObject.optString("text");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        if (!TextUtils.isEmpty(text)){
            mUscSpeechUnderstander.textUnderstander(text);
        }
    }

    public void speaking(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SPEAKING;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void speakingMsg(String[] params) {
        String json = params[0];
        String text=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            text=jsonObject.optString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(text)){
            mOnlineTTS.play(text);
        }
    }

    public void cancelSpeaking(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_CANCEL_SPEAKING;
        mHandler.sendMessage(msg);
    }

    private void cancelSpeakingMsg(String[] params) {
        mOnlineTTS.stop();
    }

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle=message.getData();
        switch (message.what) {

            case MSG_INIT:
                initMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_UPDATE_RECOGNIZER_SETTINGS:
                updateRecognizerSettings(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_START:
                start(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_STOP:
                stop(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_CANCEL:
                cancel(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_TEXT_UNDERSTANDER:
                runTextUnderstandMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SPEAKING:
                speakingMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_CANCEL_SPEAKING:
                cancelSpeakingMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            default:
                super.onHandleMessage(message);
        }
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }
}
