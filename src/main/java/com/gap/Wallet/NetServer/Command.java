package com.gap.wallet.netserver;

public enum Command {
	// client messages
	CONNECT, DISCONNECT, SET, GET, COUNT, EXISTS, REMOVE, START, COMMIT, ROLLBACK,

	// server message
	ANSWER, ERROR
}
