package org.zywx.wbpalmstar.plugin.uexunisound;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yunzhisheng.common.USCError;
import cn.yunzhisheng.nlu.basic.USCSpeechUnderstander;
import cn.yunzhisheng.nlu.basic.USCSpeechUnderstanderListener;
import cn.yunzhisheng.tts.online.basic.OnlineTTS;
import cn.yunzhisheng.tts.online.basic.TTSPlayerListener;
import cn.yunzhisheng.understander.USCUnderstanderResult;

public class EUExUnisound extends EUExBase {

    private static final String BUNDLE_DATA = "data";
    private static final int MSG_INIT = 1;
    private static final int MSG_SET_V_A_D_FRONT_TIMEOUT = 2;
    private static final int MSG_SET_BANDWIDTH = 3;
    private static final int MSG_START = 4;
    private static final int MSG_TEXT_UNDERSTANDER = 5;
    private static final int MSG_CANCEL = 6;
    private static final int MSG_SET_LANGUAGE = 7;
    private static final int MSG_SET_RECOGNIZATION_TIMEOUT = 8;
    private static final int MSG_SET_ENGINE = 9;
    private static final int MSG_STRING_RESULT = 10;
    private static final int MSG_RESPONSE_TEXT = 11;
    private static final int MSG_SET_USER_DATA = 12;
    private static final int MSG_SET_FAR_FEILD = 13;
    private static final int MSG_PLAY = 14;
    private static final int MSG_SPEAKING = 15;
    private static final int MSG_CANCEL_SPEAKING = 16;
    private static final int MSG_PAUSE_SPEAKING = 17;
    private static final int MSG_RESUME_SPEAKING = 18;
    private static final int MSG_STOP_UNDERSTANDER = 19;

    private USCSpeechUnderstander mUscSpeechUnderstander;

    private OnlineTTS mOnlineTTS;

    private Gson mGson;

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
        mGson=new Gson();
        String json = params[0];
        String appKey=null;
        String secret=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            appKey=jsonObject.optString("appKey");
            secret=jsonObject.optString("secret");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mUscSpeechUnderstander=new USCSpeechUnderstander(mContext.getApplicationContext(),appKey,secret);
        mUscSpeechUnderstander.setListener(new USCSpeechUnderstanderListener() {

            @Override
            public void onSpeechStart() {
                callBackPluginJs(JsConst.ON_SPEECH_START,"");
            }

            @Override
            public void onRecordingStart() {
                callBackPluginJs(JsConst.ON_RECORDING_START,"");
            }

            @Override
            public void onRecordingStop() {
                callBackPluginJs(JsConst.ON_RECORDING_STOP,"");
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
                callBackPluginJs(JsConst.ON_RECOGNIZER_RESULT,jsonObject.toString());
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
                callBackPluginJs(JsConst.STRING_RESULT,uscUnderstanderResult.getStringResult());
            }


            @Override
            public void onEnd(USCError uscError) {
                JSONObject jsonObject=new JSONObject();
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
        mOnlineTTS=new OnlineTTS(mContext.getApplicationContext(),appKey);
        mOnlineTTS.setTTSListener(new TTSPlayerListener() {
            @Override
            public void onPlayBegin() {

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

    public void setVADFrontTimeout(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_V_A_D_FRONT_TIMEOUT;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setVADFrontTimeoutMsg(String[] params) {
        String json = params[0];
        String frontTime=null;
        String backTime=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            frontTime=jsonObject.optString("frontTime");
            backTime=jsonObject.optString("backTime");
            if (!TextUtils.isEmpty(frontTime)&&!TextUtils.isEmpty(backTime)) {
                mUscSpeechUnderstander.setVADTimeout(Integer.valueOf(frontTime), Integer.parseInt(backTime));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setBandwidth(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_BANDWIDTH;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setBandwidthMsg(String[] params) {
        String json = params[0];
        String rate=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            rate=jsonObject.getString("rate");
            if (!TextUtils.isEmpty(rate)) {
                mUscSpeechUnderstander.setBandwidth(Integer.valueOf(rate));
            }
        } catch (JSONException e) {
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

    public void textUnderstander(String[] params) {
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

    private void textUnderstanderMsg(String[] params) {
        String json = params[0];
        String text=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            text=jsonObject.optString("text");
        } catch (JSONException e) {
        }
        if (!TextUtils.isEmpty(text)){
            mUscSpeechUnderstander.textUnderstander(text);
        }
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

    public void setLanguage(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_LANGUAGE;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setLanguageMsg(String[] params) {
        String json = params[0];
        String language;
        try {
            JSONObject jsonObject = new JSONObject(json);
            language=jsonObject.optString("language");
            if (!TextUtils.isEmpty(language)){

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRecognizationTimeout(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_RECOGNIZATION_TIMEOUT;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setRecognizationTimeoutMsg(String[] params) {
        String json = params[0];
        String time=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            time=jsonObject.optString("time");
            if (!TextUtils.isEmpty(time)){
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setEngine(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_ENGINE;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setEngineMsg(String[] params) {
        String json = params[0];
        String engine=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            engine=jsonObject.optString("engine");
            if (!TextUtils.isEmpty(engine)){
                mUscSpeechUnderstander.setEngine(engine);
            }
        } catch (JSONException e) {
        }
    }

    public void setUserData(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_USER_DATA;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setUserDataMsg(String[] params) {
        String json = params[0];
        Map<Integer,List<String>> userData;
        try {
            userData=mGson.fromJson(json,new TypeToken<Map<Integer,List<String>>>(){}.getType());
            if (userData!=null){
                mUscSpeechUnderstander.setUserData(userData);
            }
        } catch (JsonSyntaxException e) {
        }
    }

    public void setFarFeild(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_FAR_FEILD;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setFarFeildMsg(String[] params) {
        String json = params[0];
        String farFeild=null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            farFeild=jsonObject.optString("farFeild");
        } catch (JSONException e) {
        }
        if (!TextUtils.isEmpty(farFeild)){

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

    public void stopUnderstander(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_STOP_UNDERSTANDER;
        mHandler.sendMessage(msg);
    }

    private void stopUnderstanderMsg(String[] params) {
        mUscSpeechUnderstander.stop();
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
            case MSG_SET_V_A_D_FRONT_TIMEOUT:
                setVADFrontTimeoutMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_BANDWIDTH:
                setBandwidthMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_START:
                startMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_TEXT_UNDERSTANDER:
                textUnderstanderMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_CANCEL:
                cancelMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_LANGUAGE:
                setLanguageMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_RECOGNIZATION_TIMEOUT:
                setRecognizationTimeoutMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_ENGINE:
                setEngineMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_USER_DATA:
                setUserDataMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SET_FAR_FEILD:
                setFarFeildMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_SPEAKING:
                speakingMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_CANCEL_SPEAKING:
                cancelSpeakingMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_STOP_UNDERSTANDER:
                stopUnderstanderMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            default:
                super.onHandleMessage(message);        }
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

}
