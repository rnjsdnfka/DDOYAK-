package com.example.caucse.ddoyak_g;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

/*
* 앱 실행시 스플래쉬 화면을 제외하고 가장 먼저 뜨는 액티비티.
* 앱 최초 실행시 call function 관련 permission 허가 받음
* 환자 리스트는 RecyclerView로 구현.
* */


public class MainActivity extends AppCompatActivity {

    RecyclerView patientList;
    RecyclerView.LayoutManager layoutManager;
    ImageView title, yellowbar;
    ImageButton addButton;

    String name, phone;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference(Authentication.useremail);

   public static ArrayList<PatientInfo> patient_items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //사용자의 토큰 값 업데이트
        final MyFirebaseInstanceIdService fiid = new MyFirebaseInstanceIdService();
        fiid.onTokenRefresh();

        //Permission Check
        //전화 기능 Permission 허가 받음 (앱 최초 실행시)
        //Ted permission 사용
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {

            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CALL_PHONE)
                .check();


        //액티비티 제목 설정
        title = findViewById(R.id.title);
        yellowbar = findViewById(R.id.yellowbar);

        //환자리스트 - 리사이클러뷰, 레이아웃매니저 설정
        patientList = (RecyclerView) findViewById(R.id.patient_list);
        patientList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        patientList.setLayoutManager(layoutManager);

        //어댑터와 리사이클러뷰 연결
        final RecyclerAdapter myAdapter = new RecyclerAdapter(getApplicationContext(),patient_items);
        patientList.setAdapter(myAdapter);

        //환자 추가 버튼
        addButton = (ImageButton)findViewById(R.id.addButton);
        addButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),AddPatient_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        patient_items.clear();

        //firebase data 가져오기
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot patientData : dataSnapshot.getChildren()){                  //블럭 반복
                    final String titleData = patientData.getValue().toString();                     //한 블럭 전체 데이터를 한 문자열로 가져옴. ','를 토큰으로 나눠서 저장해야함.

                    DatabaseReference infoRef = database.getReference(titleData).child("INFO");

                    infoRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String infoData = dataSnapshot.getValue().toString();
                            changeViewData(infoData);
                            patient_items.add(new PatientInfo(name,phone,titleData));

                            Toast.makeText(MainActivity.this, patient_items.get(0).name, Toast.LENGTH_SHORT).show();

                            myAdapter.notifyDataSetChanged();
                            fiid.onTokenRefresh();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                myAdapter.notifyDataSetChanged();                                      //어레이리스트에 저장한 items >> 어댑터 업데이트
                fiid.onTokenRefresh();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void changeViewData(String data){
        try {
            String[] arrayTemp = data.split(",");
            int idx = arrayTemp[0].length();
            arrayTemp[0] = arrayTemp[0].substring(7,idx);
            idx = arrayTemp[1].length();
            arrayTemp[1] = arrayTemp[1].substring(6,idx-1);
            phone = arrayTemp[0];
            name = arrayTemp[1];
        }catch ( java.lang.ArrayIndexOutOfBoundsException e){
            //예외 발생 무시(리스트 업데이트에 이상 없음)
        }

    }
}