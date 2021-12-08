package com.example.crudfirebase1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crudfirebase1.models.Todo;
import com.example.crudfirebase1.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    Button btnLogin, btnLogout, btnAddTodo;
    LinearLayout contentForUser;
    TextView username, email;
    EditText addTodoItemEditText;
    User userLogged;
    ListView listToDoItem;
    int todoUpdateid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        btnLogin = findViewById(R.id.buttonLogIn);
        btnLogout = findViewById(R.id.buttonLogOut);
        contentForUser = findViewById(R.id.userCrud);
        addTodoItemEditText = findViewById(R.id.editTextAddToDo);
        listToDoItem = findViewById(R.id.listToDoItem);
        btnAddTodo = findViewById(R.id.addTodo);
        initializeGoogleSignIn();
    }

    void updateList() {
        String[] list = new String[userLogged.getTodoList().size()];

        int i = 0;
        for (Todo todo : userLogged.getTodoList()) {
            list[i] = todo.getDescription();
            i++;
        }

        Collections.reverse(Arrays.asList(list));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listToDoItem.setAdapter(adapter);

        listToDoItem.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                CharSequence[] pil = {"Edit", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(pil, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        if (j == 1) {
                            userLogged.getTodoList().remove(userLogged.getTodoList().size() - i - 1);
                            databaseReference.child("user").child(userLogged.getId()).setValue(userLogged);
                        } else {
                            todoUpdateid = userLogged.getTodoList().size() - i - 1;
                            updateTodo();
                        }
                    }
                }).create().show();

                return false;

            }
        });
    }

    private void updateTodo() {
        if (todoUpdateid==-1) return;
        addTodoItemEditText.setText(userLogged.getTodoList().get(todoUpdateid).getDescription());
        btnAddTodo.setText("simpan");
        addTodoItemEditText.requestFocus();
        addTodoItemEditText.selectAll();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account!=null)updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account) {
        btnLogout.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
        contentForUser.setVisibility(View.VISIBLE);
        databaseReference.child("user").child(account.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLogged = snapshot.getValue(User.class);
                username.setText(userLogged.getUsername());
                email.setText(userLogged.getEmail());
                updateList();

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void signIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
            checkAccount(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void checkAccount(GoogleSignInAccount account) {
        databaseReference.child("user").child(account.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) return;

                if (task.getResult().exists()) {
                    Toast.makeText(MainActivity.this, "user exists", Toast.LENGTH_SHORT).show();
                    updateUI(account);
                } else {
                    User user = new User();
                    user.setId(account.getId());
                    user.setUsername(account.getDisplayName());
                    user.setAvatar(String.valueOf(account.getPhotoUrl()));
                    user.setEmail(account.getEmail());

                    databaseReference.child("user").child(account.getId()).setValue(user);
                    updateUI(account);
                }
            }
        });
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
        username.setText("");
        email.setText("");
        btnLogin.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
        contentForUser.setVisibility(View.GONE);
    }

    public void editTodo(View view) {
    }

    public void deleteTodo(View view) {
    }

    public void addTodo(View view) {
        if(todoUpdateid!=-1){
            userLogged.getTodoList().get(todoUpdateid).setDescription(String.valueOf(addTodoItemEditText.getText()));
            todoUpdateid = -1;
            btnAddTodo.setText("Add To do");
        }else{
            Todo newTodo = new Todo();

            newTodo.setDescription(String.valueOf(addTodoItemEditText.getText()));
            userLogged.getTodoList().add(newTodo);
        }


        databaseReference.child("user").child(userLogged.getId()).setValue(userLogged);
        addTodoItemEditText.setText("");
        View view1=getCurrentFocus();
        if (view1==null){
            view1=new View(this);
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromInputMethod(view1.getWindowToken(),0);
    }
}