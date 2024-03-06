package me.athlaeos.vbreaking.hooks;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;

public abstract class PluginHook {
    private final boolean isPresent;
    public PluginHook(String name){
        this.isPresent = ValhallaModulesBreaker.getInstance().getServer().getPluginManager().getPlugin(name) != null;
    }

    public boolean isPresent(){
        return isPresent;
    }

    public abstract void whenPresent();
}
