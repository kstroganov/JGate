package com.stroganov.jgate.schemas.listeners;

import com.stroganov.jgate.Application;
import com.stroganov.jgate.schemas.FortsFutInfoRepl.*;

public interface FortsFutInfoReplListener extends ManagementMessageListener {
	int onMessage(Application.JGateListener listener, fut_sess_contents message);
}