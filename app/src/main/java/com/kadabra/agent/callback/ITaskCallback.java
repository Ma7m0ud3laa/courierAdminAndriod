package com.twoam.agent.callback;


import com.twoam.agent.model.Stop;
import com.twoam.agent.model.Task;

public interface ITaskCallback {

    void onTaskDelete(Task task);

    void onStopDelete(Stop stop);


}
