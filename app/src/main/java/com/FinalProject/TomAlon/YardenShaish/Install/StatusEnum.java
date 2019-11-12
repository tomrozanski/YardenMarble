package com.FinalProject.TomAlon.YardenShaish.Install;

public enum StatusEnum {
    WAITING, REACHED, FINISHED;

    @Override
    public String toString() {
        return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
}
