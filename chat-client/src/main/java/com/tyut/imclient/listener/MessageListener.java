package com.tyut.imclient.listener;


import com.tyut.imcommon.model.IMSendResult;

import java.util.List;

public interface MessageListener<T> {

     void process(List<IMSendResult<T>> result);

}
