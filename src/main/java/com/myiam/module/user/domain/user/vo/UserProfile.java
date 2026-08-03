package com.myiam.module.user.domain.user.vo;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * ユーザープロファイル情報
 *
 * @param familyName ユーザーの姓
 * @param givenName ユーザーの名前
 */
@ValueObject
public record UserProfile(String familyName, String givenName) {
}
