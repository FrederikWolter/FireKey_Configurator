package com.firekey.configurator.gui.components;

import javafx.scene.control.TextField;

/**
 * Simple {@link TextField} with extra max char limit option.
 */
public class TextFieldWithLengthLimit extends TextField {

    /**
     * The maximal length of the text
     */
    private int maxLength;

    public TextFieldWithLengthLimit() {
        super();
        this.maxLength = 8;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        super.replaceText(start, end, text);
        verify();
    }

    @Override
    public void replaceSelection(String text) {
        super.replaceSelection(text);
        verify();
    }

    /**
     * Updates the string to the {@link #maxLength} char limit. <br>
     * Also repositions the caret at the end of the string.
     */
    private void verify() {
        if (getText().length() > maxLength) {
            setText(getText().substring(0, maxLength));
            positionCaret(maxLength);
        }
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

}
