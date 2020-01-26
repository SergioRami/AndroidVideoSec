package ramar.videosec;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by crafter on 22/05/16.
 */
public class CamerasAdapter extends ArrayAdapter<Camera> {

    public CamerasAdapter (Context context){
        super(context, android.R.layout.simple_list_item_1);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1,parent,false);
        }

        Camera camera = getItem(position);

        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(camera.getName());

        return convertView;
    }
}
