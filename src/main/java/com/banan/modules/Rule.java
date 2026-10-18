package com.banan.modules;

import org.bukkit.event.Event;

public abstract class Rule implements ruleInterface{

    public abstract void handleEvent(Event event);
    public abstract boolean isEventApplied();

    private boolean isActive = false;
    protected  boolean isEventApplied = false;
    private int rulePriority = 0;

    @Override 
    public void setIsActive(boolean isActive){
        this.isActive = isActive;
    }

    @Override 
    public boolean isActive(){
        return  this.isActive;
    }

    @Override 
    public void setRulePriority(int rulePriority){
        this.rulePriority = rulePriority;
    }

    @Override 
    public int getRulePriority(){
        return this.rulePriority;
    }

}
