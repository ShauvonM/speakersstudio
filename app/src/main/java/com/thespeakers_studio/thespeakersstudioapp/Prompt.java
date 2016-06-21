package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

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
    private ArrayList<PromptAnswer> answer;
    private boolean isOpen;
    private boolean required;
    private String referenceDefault;

    public Prompt(int id, int step, int order, int type, String text, boolean req, int charLimit, String refDefault) {
        this.id = id;
        this.step = step;
        this.order = order;
        this.type = type;
        this.text = text;
        this.processedText = text;
        this.charLimit = charLimit;
        this.isOpen = false;
        this.required = req;
        this.referenceDefault = refDefault;

        this.answer = new ArrayList<>();
    }
    // no req or char limit
    public Prompt(int id, int step, int order, int type, String text) {
        this(id, step, order, type, text, true, 0, "");
    }
    // with req but no charlimit
    public Prompt(int id, int step, int order, int type, String text, boolean req) {
        this(id, step, order, type, text, req, 0, "");
    }
    // with charlimit but no req
    public Prompt(int id, int step, int order, int type, String text, int charLimit) {
        this(id, step, order, type, text, true, charLimit, "");
    }
    // with charLimit and reference default, but no req
    public Prompt(int id, int step, int order, int type, String text, int charLimit, String refDef) {
        this(id, step, order, type, text, true, charLimit, refDef);
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
    public void setOrder(int o) {
        order = o;
    }
    public int getCharLimit() {
        return this.charLimit;
    }

    public int getType() {
        return this.type;
    }

    public boolean getIsPrompt() {
        return this.type != PresentationData.HEADER && this.type != PresentationData.NEXT;
    }

    public String getText() {
        return this.text;
    }

    public ArrayList<PromptAnswer> getAnswer() {
        return this.answer;
    }

    public PromptAnswer getAnswerByKey(String key) {
        if (key.isEmpty()) {
            return new PromptAnswer();
        }
        ArrayList<PromptAnswer> answers = getAnswer();
        for (PromptAnswer answer : answers) {
            if (answer.getKey().equals(key)) {
                return answer;
            }
        }
        return new PromptAnswer();
    }
    public PromptAnswer getAnswerById(String id) {
        if (id.isEmpty()) {
            return new PromptAnswer();
        }
        ArrayList<PromptAnswer> answers = getAnswer();
        for (PromptAnswer answer : answers) {
            if (answer.getId().equals(id)) {
                return answer;
            }
        }
        return new PromptAnswer();
    }

    public void setAnswers(ArrayList<PromptAnswer> answers) {
        /*
            for each answer on this prompt:

            if it has no id, remove it

            if it has an id, check it against answers {

                if not in answers, set value to empty string

            }

            anything left in answers, call addAnswer
         */

        if (getAnswer().size() > answers.size()) {
            for (PromptAnswer savedAnswer : getAnswer()) {
                savedAnswer.setValue("");
            }
        }
        for (PromptAnswer answer : answers) {
            addAnswer(answer);
        }

        /*
        for (PromptAnswer savedAnswer : getAnswer()) {
            if (savedAnswer.getId().isEmpty()) {
                // uh oh!
                Log.e("SS", "An existing answer has no ID. How could that happen?");
            } else {
                boolean inAnswers = false;
                for (int cnt = 0; cnt < answers.size(); cnt++) {
                    PromptAnswer newAnswer = answers.get(cnt);
                    if (newAnswer.getId().equals(savedAnswer.getId())) {
                        cnt = answer.size();
                        inAnswers = true;
                    }
                }

                if (!inAnswers) {
                    // this saved answer isn't in the new list we want to save, so kill it
                    savedAnswer.setValue("");
                }
            }
        }
        if (answers.size() > 0) {
            for (PromptAnswer answer : answers) {
                addAnswer(answer);
            }
        }
        */
    }

    public void addAnswer(PromptAnswer answer) {
        PromptAnswer existingByKey = getAnswerByKey(answer.getKey());
        PromptAnswer existingById = getAnswerById(answer.getId());

        Log.d("SS", "Add answer '" + answer.getId() + "' " + answer.getKey() + " " + answer.getValue());

        if (!existingByKey.existsInDB() && !existingById.existsInDB()) {
            this.answer.add(answer);
        } else {
            if (!existingById.existsInDB() || existingByKey.getId().equals(answer.getId())) {
                // the answer we are saving is the same as the existing key, so we can just update
                // the value
                existingByKey.setValue(answer.getValue());
            } else {
                /*
                We are saving an existing item to a new key.
                Therefore, we have to clear the value of the existing key, to remove it from the DB
                And we have to set the new value and key to the answer with the given ID
                 */
                existingByKey.setValue("");
                existingById.setKey(answer.getKey());
                existingById.setValue(answer.getValue());
            }
        }
        Collections.sort(this.answer, new Comparator<PromptAnswer>() {
            @Override
            public int compare(PromptAnswer lhs, PromptAnswer rhs) {
                return lhs.getKey().compareTo(rhs.getKey());
            }
        });
    }
    public void addAnswer(String key, String value) {
        PromptAnswer answer = new PromptAnswer(key, value, getId());
        addAnswer(answer);
    }

    public boolean getRequired() { return this.required; }

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

    public String getReferenceDefault() {
        return referenceDefault;
    }
}
