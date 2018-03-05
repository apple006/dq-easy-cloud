package com.dq.easy.cloud.model.common.encrypt.sm3.pojo.bo;


import com.dq.easy.cloud.model.common.encrypt.sm3.utils.DqSM3Utils;

/**
 * 
 * <p>
 * SM3业务逻辑对象
 * </p>
 *
 * @author daiqi 创建时间 2018年2月23日 下午2:21:07
 */
public class DqSM3DigestBO {
	/** SM3值的长度 */
	private static final int BYTE_LENGTH = 32;

	/** SM3分组长度 */
	private static final int BLOCK_LENGTH = 64;

	/** 缓冲区长度 */
	private static final int BUFFER_LENGTH = BLOCK_LENGTH * 1;

	/** 缓冲区 */
	private byte[] xBuf = new byte[BUFFER_LENGTH];

	/** 缓冲区偏移量 */
	private int xBufOff;

	/** 初始向量 */
	private byte[] V = DqSM3Utils.iv.clone();

	private int cntBlock = 0;

	public DqSM3DigestBO() {
		
	}
	
	public static DqSM3DigestBO newInstance(){
		return new DqSM3DigestBO();
	}
	
	public DqSM3DigestBO(DqSM3DigestBO dqSM3DigestDTO) {
		System.arraycopy(dqSM3DigestDTO.xBuf, 0, this.xBuf, 0, dqSM3DigestDTO.xBuf.length);
		this.xBufOff = dqSM3DigestDTO.xBufOff;
		System.arraycopy(dqSM3DigestDTO.V, 0, this.V, 0, dqSM3DigestDTO.V.length);
	}

	/**
	 * SM3结果输出
	 *
	 * @param out
	 *            保存SM3结构的缓冲区
	 * @param outOff
	 *            缓冲区偏移量
	 * @return 字节长度
	 */
	public int doFinal(byte[] out, int outOff) {
		byte[] tmp = doFinal();
		System.arraycopy(tmp, 0, out, 0, tmp.length);
		return BYTE_LENGTH;
	}

	/**
	 * 重置
	 */
	public void reset() {
		xBufOff = 0;
		cntBlock = 0;
		V = DqSM3Utils.iv.clone();
	}

	/**
	 * 明文输入
	 *
	 * @param in
	 *            明文输入缓冲区
	 * @param inOff
	 *            缓冲区偏移量
	 * @param len
	 *            明文长度
	 */
	public void update(byte[] in, int inOff, int len) {
		int partLen = BUFFER_LENGTH - xBufOff;
		int inputLen = len;
		int dPos = inOff;
		if (partLen < inputLen) {
			System.arraycopy(in, dPos, xBuf, xBufOff, partLen);
			inputLen -= partLen;
			dPos += partLen;
			doUpdate();
			while (inputLen > BUFFER_LENGTH) {
				System.arraycopy(in, dPos, xBuf, 0, BUFFER_LENGTH);
				inputLen -= BUFFER_LENGTH;
				dPos += BUFFER_LENGTH;
				doUpdate();
			}
		}

		System.arraycopy(in, dPos, xBuf, xBufOff, inputLen);
		xBufOff += inputLen;
	}

	/**
	 * 更新
	 */
	private void doUpdate() {
		byte[] B = new byte[BLOCK_LENGTH];
		for (int i = 0; i < BUFFER_LENGTH; i += BLOCK_LENGTH) {
			System.arraycopy(xBuf, i, B, 0, B.length);
			doHash(B);
		}
		xBufOff = 0;
	}

	/**
	 * 转16进制
	 * 
	 * @param B
	 *            字节数组
	 */
	private void doHash(byte[] B) {
		byte[] tmp = DqSM3Utils.CF(V, B);
		System.arraycopy(tmp, 0, V, 0, V.length);
		cntBlock++;
	}

	private byte[] doFinal() {
		byte[] B = new byte[BLOCK_LENGTH];
		byte[] buffer = new byte[xBufOff];
		System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
		byte[] tmp = DqSM3Utils.padding(buffer, cntBlock);
		for (int i = 0; i < tmp.length; i += BLOCK_LENGTH) {
			System.arraycopy(tmp, i, B, 0, B.length);
			doHash(B);
		}
		return V;
	}

	public void update(byte in) {
		byte[] buffer = new byte[] { in };
		update(buffer, 0, 1);
	}

	public int getDigestSize() {
		return BYTE_LENGTH;
	}

}