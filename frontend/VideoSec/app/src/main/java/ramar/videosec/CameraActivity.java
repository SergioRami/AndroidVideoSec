package ramar.videosec;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;

public class CameraActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private UserCameraAsyncTask mCameraTask = null;

    private CamerasAdapter mAdapter;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");

        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(this);
        mAdapter = new CamerasAdapter(this);
        list.setAdapter(mAdapter);

        new UserCameraAsyncTask(this,mAdapter,mEmail).execute();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Camera camera = mAdapter.getItem(position);

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("email",mEmail);
        intent.putExtra("id",camera.getId());
        startActivity(intent);
    }
}
