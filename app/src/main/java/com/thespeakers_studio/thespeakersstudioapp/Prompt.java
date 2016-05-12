package com.thespeakers_studio.thespeakersstudioapp;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class Prompt {
    private int id;
    private int step;
    private int order;
    private int charLimit;
    private int type;
    private String text;
    private String processedText;
    private String answer;
    private boolean isOpen;

    public Prompt(int id, int step, int order, int type, String text, int charLimit) {
        this.id = id;
        this.step = step;
        this.order = order;
        this.type = type;
        this.text = text;
        this.processedText = text;
        this.charLimit = charLimit;
        this.answer = "";
        this.isOpen = false;
    }
    public Prompt(int id, int step, int order, int type, String text) {
        this(id, step, order, type, text, 0);
    }

    public int getId() {
        return this.id;
    }
    public int getStep() {
        return this.step;
    }
    public int getOrder() {
        return this.order;
    }
    public int getCharLimit() {
        return this.charLimit;
    }
    public int getType() {
        return this.type;
    }
    public String getText() {
        return this.text;
    }
    public String getAnswer() {
        return this.answer;
    }
    public void setAnswer(String a) {
        this.answer = a;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProcessedText() { return this.processedText; }
    public void setProcessedText(String text) { this.processedText = text; }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean getIsOpen() {
        return this.isOpen;
    }
    public void toggleOpen() {
        this.isOpen = !this.isOpen;
    }
}
