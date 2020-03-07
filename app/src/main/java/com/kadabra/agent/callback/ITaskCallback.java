package com.kadabra.agent.callback;


import com.kadabra.agent.model.Stop;
import com.kadabra.agent.model.Task;

public interface ITaskCallback {

    void onTaskDelete(Task task);

    void onStopDelete(Stop stop);


}
