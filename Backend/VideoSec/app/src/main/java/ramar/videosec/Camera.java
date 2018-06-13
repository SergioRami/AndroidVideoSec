package ramar.videosec;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by crafter on 22/05/16.
 */
public class Camera {

    private String mName;
    private int mId;

    public Camera(JSONObject json) throws JSONException {
        mName = json.getString("name");
        mId = json.getInt("id");
    }

    public Camera(String mName, int mId){
        this.mId = mId;
        this.mName = mName;
    }

    public String getName(){
        return mName;
    }
    public int getId(){
        return mId;
    }
}
