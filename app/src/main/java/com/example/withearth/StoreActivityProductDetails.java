package com.example.withearth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class StoreActivityProductDetails extends AppCompatActivity {
    private ImageView productImage;
    private TextView productPrice;
    private TextView productName;
    private TextView productDescription;
    private Button addToCartButton;
    private Button purchaseButton;
    private ElegantNumberButton numberButton;
    Context context;

    private FirebaseAuth auth;


    private Intent intent;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_store_product_details);

        numberButton = (ElegantNumberButton) findViewById(R.id.number_btn);

        //intent로 전달된 상품 정보 받아오기
        String pName = getIntent().getStringExtra("name");
        String pPrice = getIntent().getStringExtra("price");
        String pDescription = getIntent().getStringExtra("description");
        String pImage = getIntent().getStringExtra("image");

        productImage = (ImageView) findViewById(R.id.product_image_details);
        productDescription = (TextView) findViewById(R.id.product_description_details);
        productName = (TextView) findViewById(R.id.product_name_details);
        productPrice = (TextView) findViewById(R.id.product_price_details);

        addToCartButton = (Button) findViewById(R.id.add_to_cart_btn);
        purchaseButton = (Button) findViewById(R.id.purchase_btn);

        productName.setText(pName);
        productPrice.setText(pPrice);
        productDescription.setText(pDescription);
        Picasso.get().load(pImage).into(productImage);

        //장바구니를 거치지 않고 바로 주문하기

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //시간 정보 받아오기
                    String saveCurrentTime, saveCurrentDate;
                    Calendar calForDate = Calendar.getInstance();

                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                    saveCurrentDate = currentDate.format(calForDate.getTime());

                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                    saveCurrentTime = currentDate.format(calForDate.getTime());

                    final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

                    //구매 정보 저장 시 realtime database 사용, Cart List 밑에 User View와 Admin View 생성
                    final HashMap<String, Object> cartMap = new HashMap<>();
                    cartMap.put("name", pName);
                    cartMap.put("price", pPrice);
                    cartMap.put("image", pImage);
                    String pQuantity = numberButton.getNumber();
                    cartMap.put("quantity", pQuantity);

                    cartListRef.child("User View").child(auth.getCurrentUser().getUid()).child("Products").child(pName)
                            .updateChildren(cartMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        cartListRef.child("Admin View").child(auth.getCurrentUser().getUid())
                                                .child("Products").child(pName)
                                                .updateChildren(cartMap);

                                    }
                                }
                            });
                    // 상품 구매 시 장바구니 거치지 않고 바로 StoreActivityConfirmOrder로 이동
                Intent intent = new Intent(StoreActivityProductDetails.this, StoreActivityConfirmOrder.class);

                //상품 금액 계산, intent로 정보 넘기기
                int iQuantity = Integer.valueOf(pQuantity).intValue();
                int iPrice = Integer.valueOf(pPrice).intValue();
                int tPrice = iQuantity * iPrice;
                String sPrice = String.valueOf(tPrice);
                intent.putExtra("Total Price", sPrice);
                startActivity(intent);
            }
        });

        //장바구니를 거쳐서 주문하기, 바로 페이지 넘어가지 않고 뒤로가기 -> 상품목록에서 장바구니 버튼 눌러야함

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String saveCurrentTime, saveCurrentDate;
                Calendar calForDate = Calendar.getInstance();

                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                saveCurrentTime = currentDate.format(calForDate.getTime());

                final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

                final HashMap<String, Object> cartMap = new HashMap<>();
                cartMap.put("name", pName);
                cartMap.put("price", pPrice);
                cartMap.put("image", pImage);
                cartMap.put("quantity", numberButton.getNumber());

                cartListRef.child("User View").child(auth.getCurrentUser().getUid()).child("Products").child(pName)
                        .updateChildren(cartMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    cartListRef.child("Admin View").child(auth.getCurrentUser().getUid())
                                            .child("Products").child(pName)
                                            .updateChildren(cartMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast myToast = Toast.makeText(StoreActivityProductDetails.this, "장바구니에 추가되었습니다.", Toast.LENGTH_SHORT);
                                                        myToast.show();

                                                    }


                                                }
                                            });

                                }
                            }
                        });
            }
        });


    }
}

