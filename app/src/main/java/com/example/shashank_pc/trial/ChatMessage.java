package com.example.shashank_pc.trial;

/**
 * Created by shashank-pc on 8/31/2017.
 */

public class ChatMessage {

    private boolean position;   //Position of chat message
    private String message;     //Message text of chat message
    private String creator;     //creator of chat message

    public ChatMessage(){}

    public ChatMessage(boolean postion, String message, String creator)
    {
        this.message=message;
        this.position=postion;
        this.creator=creator;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPosition() {
        return position;
    }

    public String getCreator()
    {
        return creator;
    }

    public void setPosition(boolean position)
    {
        this.position=position;
    }

    public void setMessage(String message)
    {
        this.message=message;
    }
    public void setCreator(String creator)
    {
        this.creator=creator;
    }


}
