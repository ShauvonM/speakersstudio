package com.thespeakers_studio.thespeakersstudioapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class Prompt implements Parcelable {
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
    private int referenceId;

    private String answerId;

    public Prompt(int id, int step, int order, int type, String text, boolean req, int charLimit, int ref, String refDefault) {
        this.id = id;
        this.step = step;
        this.order = order;
        this.type = type;
        this.text = text;
        this.processedText = text;
        this.charLimit = charLimit;
        this.isOpen = false;
        this.required = req;
        this.referenceId = ref;
        this.referenceDefault = refDefault;

        this.answer = new ArrayList<>();

        this.answerId = "";
    }
    // no req or char limit
    public Prompt(int id, int step, int order, int type, String text) {
        this(id, step, order, type, text, true, 0, 0, "");
    }
    // with req but no charlimit
    public Prompt(int id, int step, int order, int type, String text, boolean req) {
        this(id, step, order, type, text, req, 0, 0, "");
    }
    // with charlimit but no req
    public Prompt(int id, int step, int order, int type, String text, int charLimit) {
        this(id, step, order, type, text, true, charLimit, 0, "");
    }
    // with charLimit and reference default, but no req
    public Prompt(int id, int step, int order, int type, String text, int charLimit, int ref, String refDef) {
        this(id, step, order, type, text, true, charLimit, ref, refDef);
    }

    public String getAnswerId() {
        return this.answerId;
    }
    public void setAnswerId(String answerId) {
        this.answerId = answerId;
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

    public ArrayList<PromptAnswer> getAnswerIgnoreEmpty() {
        ArrayList<PromptAnswer> actualAnswers = new ArrayList<>();
        for (PromptAnswer answer : this.answer) {
            if (!answer.getValue().isEmpty()) {
                actualAnswers.add(answer);
            }
        }
        return actualAnswers;
    }

    public Prompt clone(String answerId) {
        Prompt p = new Prompt(this.id, this.step, this.order, this.type, this.text, this.required, this.charLimit, this.referenceId, this.referenceDefault);
        if (answerId.isEmpty()) {
            p.setAnswers(this.getAnswer());
        } else {
            p.setAnswerId(answerId);
            p.setAnswers(this.getAnswersByLinkId(answerId));
        }
        return p;
    }
    public Prompt clone() {
        return clone("");
    }

    public PromptAnswer getAnswerByKey(String key) {
        if (key.isEmpty()) {
            return new PromptAnswer();
        }
        ArrayList<PromptAnswer> answers = getAnswer();
        for (PromptAnswer answer : answers) {
            if (answer.getKey().equals(key) && !answer.getValue().isEmpty()) {
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
    public ArrayList<PromptAnswer> getAnswersByLinkId(String id) {
        ArrayList<PromptAnswer> answers = new ArrayList<>();
        for (PromptAnswer answer : getAnswer()) {
            if (answer.getAnswerLinkId().equals(id)) {
                answers.add(answer);
            }
        }
        return answers;
    }

    public void resetAnswers() {
        this.answer = new ArrayList<>();
    }

    public void setAnswers(ArrayList<PromptAnswer> answers) {
        // when setting a new set of answers, we should first clear out all of the existing ones
        // keep the ID's so the DB will know which ones to delete
        for (PromptAnswer savedAnswer : getAnswer()) {
            savedAnswer.setValue("");
        }
        // add answer will handle overwriting the existing dudes with new data where necessary
        for (PromptAnswer answer : answers) {
            addAnswer(answer);
        }
    }

    public void addAnswer(PromptAnswer answer) {
        if (answer == null) {
            return;
        }

        PromptAnswer existingById = getAnswerById(answer.getId());

        // if we are saving a thing with an existing ID, we will just update that ID
        // this is for list items, so we don't lose track of IDs
        if (existingById.existsInDB()) {
            existingById.setValue(answer.getValue());
            existingById.setKey(answer.getKey());
            existingById.setAnswerLinkId(this.getAnswerId());
        } else {
            // if the ID doesn't already exist, we can just save this
            doAddAnswer(answer);
        }

        Collections.sort(this.answer, new Comparator<PromptAnswer>() {
            @Override
            public int compare(PromptAnswer lhs, PromptAnswer rhs) {
                return lhs.getKey().compareTo(rhs.getKey());
            }
        });
    }
    private void doAddAnswer(PromptAnswer answer) {
        if (!this.getAnswerId().isEmpty()) {
            answer.setAnswerLinkId(this.getAnswerId());
        }

        this.answer.add(answer);
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

    public int getReferenceId() {
        return referenceId;
    }
    public String getReferenceDefault() {
        return referenceDefault;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(step);
        dest.writeInt(order);
        dest.writeInt(charLimit);
        dest.writeInt(type);
        dest.writeString(text);
        dest.writeString(processedText);
        dest.writeTypedList(answer);
        dest.writeByte((byte)(isOpen ? 1 : 0));
        dest.writeByte((byte)(required ? 1 : 0));
        dest.writeString(referenceDefault);
        dest.writeInt(referenceId);
        dest.writeString(answerId);
    }

    public Prompt(Parcel in) {
        id = in.readInt();
        step = in.readInt();
        order = in.readInt();
        charLimit = in.readInt();
        type = in.readInt();
        text = in.readString();
        processedText = in.readString();
        answer = in.createTypedArrayList(PromptAnswer.CREATOR);
        isOpen = in.readByte() == 1;
        required = in.readByte() == 1;
        referenceDefault = in.readString();
        referenceId = in.readInt();
        answerId = in.readString();
    }

    public static final Parcelable.Creator<Prompt> CREATOR = new Creator<Prompt>() {
        @Override
        public Prompt createFromParcel(Parcel source) {
            return new Prompt(source);
        }

        @Override
        public Prompt[] newArray(int size) {
            return new Prompt[size];
        }
    };
}
