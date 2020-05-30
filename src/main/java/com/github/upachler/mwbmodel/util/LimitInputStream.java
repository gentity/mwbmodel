/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.util;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Uwe Pachler
 */
public class LimitInputStream extends InputStream {
	
	private final InputStream is;
	private long limit;
	private final boolean fakeClose;

	public LimitInputStream(InputStream is, long limit, boolean fakeClose) {
		this.is = is;
		this.limit = limit;
		this.fakeClose = fakeClose;
	}

	@Override
	public int read() throws IOException {
		byte[] b = {0};
		int n = this.read(b, 0, 1);
		if (n == 1) {
			return b[0];
		} else {
			return -1;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (limit <= 0) {
			return -1;
		}
		len = maskByLimit(len);
		int n = is.read(b, off, len);
		limit -= n;
		return n;
	}

	private long maskByLimit(long n) {
		return Math.min((long) n, limit);
	}

	private int maskByLimit(int n) {
		return (int) Math.min((long) n, limit);
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public void close() throws IOException {
		if (!fakeClose) {
			is.close();
		}
	}

	@Override
	public int available() throws IOException {
		return maskByLimit(is.available());
	}

	@Override
	public long skip(long n) throws IOException {
		n = maskByLimit(n);
		long skipped = is.skip(n);
		limit -= skipped;
		return skipped;
	}
	
}
