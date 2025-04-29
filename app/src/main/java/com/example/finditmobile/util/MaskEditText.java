package com.example.finditmobile.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskEditText {

    public static void insertPhoneMask(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;
            String oldString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");

                String maskedStr = "";
                if (isUpdating) {
                    oldString = str;
                    isUpdating = false;
                    return;
                }

                int length = str.length();

                if (length > 11)
                    str = str.substring(0, 11);

                int i = 0;

                if (length > 0) {
                    maskedStr += "(" + str.substring(0, Math.min(2, length));
                    if (length > 2) {
                        maskedStr += ") ";
                        if (length <= 6) {
                            maskedStr += str.substring(2, length);
                        } else {
                            maskedStr += str.substring(2, 7) + "-" + str.substring(7, length);
                        }
                    }
                }

                isUpdating = true;
                editText.setText(maskedStr);
                editText.setSelection(editText.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
