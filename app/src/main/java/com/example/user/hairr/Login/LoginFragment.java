package com.example.user.hairr.Login;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.hairr.HomeAdmin;
import com.example.user.hairr.HomeCustomer;
import com.example.user.hairr.HomeStylist;
import com.example.user.hairr.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements OnLoginListener {

   private FirebaseAuth auth;
   private DatabaseReference mDatabaseRef;
   private  EditText edtEmail,edtPassword;
   private Button login;
   private ProgressDialog dialog;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        auth = FirebaseAuth.getInstance();
        edtEmail = (EditText)root.findViewById(R.id.loginEmail);
        edtPassword = (EditText)root.findViewById(R.id.loginPassword);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dialog = new ProgressDialog(getContext());

        login = (Button)root.findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startLoginIn();
                Toast.makeText(getActivity(), "it worked", Toast.LENGTH_SHORT).show();
            }
        });





        return root;
    }

    private void startLoginIn() {

        dialog.setMessage("Login in... please wait");
        dialog.show();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty()||password.isEmpty()){
            Toast.makeText(getContext(), "Please enter these field to continue", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            dialog.dismiss();
                            String uid = auth.getCurrentUser().getUid();

                            mDatabaseRef.child(uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String status = dataSnapshot.child("status").getValue().toString();

                                            if (status.equalsIgnoreCase("customer")){
                                                startActivity(new Intent(getContext(), HomeCustomer.class));
                                            }else if (status.equalsIgnoreCase("stylist")){
                                                startActivity(new Intent(getContext(), HomeStylist.class));
                                            }else if (status.equalsIgnoreCase("admin")){
                                                startActivity(new Intent(getContext(), HomeAdmin.class));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                dialog.dismiss();
                Toast.makeText(getContext(), String.valueOf(e), Toast.LENGTH_SHORT).show();

            }
        });



    }

    @Override
    public void login() {

    }
}
