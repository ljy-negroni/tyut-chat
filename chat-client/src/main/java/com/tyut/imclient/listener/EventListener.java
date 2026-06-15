package com.tyut.imclient.listener;

import com.tyut.imcommon.model.IMUserEvent;

import java.util.List;

public interface EventListener {

     void process(List<IMUserEvent> event);

}
