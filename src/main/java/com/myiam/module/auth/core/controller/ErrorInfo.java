package com.myiam.module.auth.core.controller;

/**
 * エラー情報。
 *
 * @param errorCode エラーコード
 * @param description エラー説明
 */
record ErrorInfo(String errorCode, String description) {
}
