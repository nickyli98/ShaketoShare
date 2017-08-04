package ic.hku.hk;

import android.app.Activity;
import android.app.Service;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class PasswordInput implements View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {

    private final Activity context;
    private final EditText first;
    private final EditText second;
    private final EditText third;
    private final EditText fourth;
    private final EditText hiddenText;

    public PasswordInput(Activity context, EditText first, EditText second, EditText third, EditText fourth, EditText hiddenText) {
        this.context = context;
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.hiddenText = hiddenText;
        initializeListeners();
    }

    private void initializeListeners() {
        hiddenText.addTextChangedListener(this);

        first.setOnFocusChangeListener(this);
        second.setOnFocusChangeListener(this);
        third.setOnFocusChangeListener(this);
        fourth.setOnFocusChangeListener(this);

        first.setOnKeyListener(this);
        second.setOnKeyListener(this);
        third.setOnKeyListener(this);
        fourth.setOnKeyListener(this);
        hiddenText.setOnKeyListener(this);
    }

    public void showSoftKeyboard(EditText editText) {
        if (editText == null){
            return;
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    public void hideSoftKeyboard(EditText editText) {
        if (editText == null){
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public String getPassword() {
        if(hiddenText.getText().length() == 4){
            return hiddenText.getText().toString();
        } else {
            return null;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        final int id = view.getId();
        if (hasFocus) {
            switch (id) {
                case (R.id.first_pin_R):
                    showSoftKeyboard(hiddenText);
                    break;
                case (R.id.second_pin_R):
                    showSoftKeyboard(hiddenText);
                    break;
                case (R.id.third_pin_R):
                    showSoftKeyboard(hiddenText);
                    break;
                case (R.id.fourth_pin_R):
                    hideSoftKeyboard(hiddenText);
                    break;
            }
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL
                && view.getId() == hiddenText.getId()){
            switch(hiddenText.getText().length()) {
                case (4):
                    fourth.setText("");
                    break;
                case (3):
                    third.setText("");
                    break;
                case (2):
                    second.setText("");
                    break;
                case (1):
                    first.setText("");
                    break;
            }
            if(hiddenText.getText().length() > 0){
                hiddenText.setText(hiddenText.getText().subSequence(0, hiddenText.length() - 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        switch (charSequence.length()){
            case (0):
                first.setText("");
                break;
            case (1):
                first.setText(charSequence.charAt(0) + "");
                second.setText("");
                third.setText("");
                fourth.setText("");
                break;
            case (2):
                second.setText(charSequence.charAt(1) + "");
                third.setText("");
                fourth.setText("");
                break;
            case (3):
                third.setText(charSequence.charAt(2) + "");
                fourth.setText("");
                break;
            case (4):
                fourth.setText(charSequence.charAt(3) + "");
                hideSoftKeyboard(fourth);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}
}
