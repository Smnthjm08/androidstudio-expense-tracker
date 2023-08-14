package com.example.expense_tracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expense_tracker.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class DashBoardFragment extends Fragment {
    //animation class object
    private Animation FadOpen, FadClose;

    //Floating button
    private FloatingActionButton fab_main_btn, fab_income_btn, fab_expense_btn;

    //Floating button textview
    private TextView fab_income_txt, fab_expense_txt;

    //boolean
    private boolean isOpen = false;

    //Dashboard income and expense Results..
    private TextView totalIncomeResult, totalExpenseResult;

    //Firebase...
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    //Recycler view
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dash_board, container, false);

        //creating firebase for income and expense amount
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);
        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

        //connecting floating button with layout
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_Ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_Ft_btn);

        // connecting floating text with layout
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);

        //Total income and expense connect to main
        totalIncomeResult = myview.findViewById(R.id.income_set_result);
        totalExpenseResult = myview.findViewById(R.id.expense_set_result);

        //Recycler connect
        mRecyclerIncome = myview.findViewById(R.id.recycler_income);
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense);

        //animation connect
        FadOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        FadClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftAnimation();
            }
        });

        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomeDataInsert();
            }
        });

        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseDataInsert();
            }
        });

        //Calculate total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalIncome = 0;
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    totalIncome += data.getAmount();
                }
                totalIncomeResult.setText(String.valueOf(totalIncome+".00"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //Calculate total expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalExpense = 0;
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    totalExpense += data.getAmount();
                }
                totalExpenseResult.setText(String.valueOf(totalExpense+".00"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //Recycler
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerIncome.setReverseLayout(true);
        layoutManagerIncome.setStackFromEnd(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerExpense.setReverseLayout(true);
        layoutManagerExpense.setStackFromEnd(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);

        return myview;
    }

    //Floating button animation
    private void ftAnimation() {
        if (isOpen) {
            fab_income_btn.startAnimation(FadClose);
            fab_expense_btn.startAnimation(FadClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadClose);
            fab_expense_txt.startAnimation(FadClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen = false;
        } else {
            fab_income_btn.startAnimation(FadOpen);
            fab_expense_btn.startAnimation(FadOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadOpen);
            fab_expense_txt.startAnimation(FadOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen = true;
        }
    }

    private void incomeDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myview);
        AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText editAmount = myview.findViewById(R.id.ammount_edit);
        EditText editType = myview.findViewById(R.id.type_edit);
        EditText editNote = myview.findViewById(R.id.note_edit);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                String type = editType.getText().toString().trim();
                String amount = editAmount.getText().toString().trim();
                String note = editNote.getText().toString().trim();

                if (TextUtils.isEmpty(type)) {
                    editType.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    editAmount.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    editNote.setError("Required Field...");
                    return;
                }

                int ourAmountInt = Integer.parseInt(amount);
                //push data into database and generate random id
                String id = mIncomeDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());                 //this is to get the current date
                Data data = new Data(ourAmountInt, type, note, id, mDate);
                mIncomeDatabase.child(id).setValue(data);                                       //data is getting added into database
                Toast.makeText(getActivity(), "Data ADDED", Toast.LENGTH_SHORT).show();
                ftAnimation();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftAnimation();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void expenseDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myview);
        AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText editAmount = myview.findViewById(R.id.ammount_edit);
        EditText editType = myview.findViewById(R.id.type_edit);
        EditText editNote = myview.findViewById(R.id.note_edit);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = editType.getText().toString().trim();
                String amount = editAmount.getText().toString().trim();
                String note = editNote.getText().toString().trim();

                if (TextUtils.isEmpty(type)) {
                    editType.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    editAmount.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    editNote.setError("Required Field...");
                    return;
                }

                //push data into database and generate random id
                int ourAmountInt = Integer.parseInt(amount);
                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());                  //this is to get the current date
                Data data = new Data(ourAmountInt, type, note, id, mDate);
                mExpenseDatabase.child(id).setValue(data);                                      //data is getting added into database
                Toast.makeText(getActivity(), "Data ADDED", Toast.LENGTH_SHORT).show();
                ftAnimation();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftAnimation();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Data> incomeOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();
        FirebaseRecyclerAdapter<Data, IncomeViewHolder> incomeAdapter =
                new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(incomeOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                        holder.setIncomeAmount(model.getAmount());
                        holder.setIncomeType(model.getType());
                        holder.setIncomeDate(model.getDate());
                    }

                    @NonNull
                    @Override
                    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
                        return new IncomeViewHolder(view);
                    }
                };
        incomeAdapter.startListening();
        mRecyclerIncome.setAdapter(incomeAdapter);

        FirebaseRecyclerOptions<Data> expenseOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class)
                        .build();
        FirebaseRecyclerAdapter<Data, ExpenseViewHolder> expenseAdapter =
                new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(expenseOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                        holder.setExpenseAmount(model.getAmount());
                        holder.setExpenseType(model.getType());
                        holder.setExpenseDate(model.getDate());
                    }

                    @NonNull
                    @Override
                    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false);
                        return new ExpenseViewHolder(view);
                    }
                };
        expenseAdapter.startListening();
        mRecyclerExpense.setAdapter(expenseAdapter);
    }

    //For income data
    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setIncomeAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.ammount_Income_ds);
            mAmount.setText(String.valueOf(amount));
        }

        public void setIncomeType(String type) {
            TextView mType = mView.findViewById(R.id.type_Income_ds);
            mType.setText(type);
        }

        public void setIncomeDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_income_ds);
            mDate.setText(date);
        }
    }

    //For expense data
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setExpenseAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.ammount_expense_ds);
            mAmount.setText(String.valueOf(amount));
        }

        public void setExpenseType(String type) {
            TextView mType = mView.findViewById(R.id.type_expense_ds);
            mType.setText(type);
        }

        public void setExpenseDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_expense_ds);
            mDate.setText(date);
        }
    }
}
