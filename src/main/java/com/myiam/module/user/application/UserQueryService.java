package com.myiam.module.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザークエリサービス
 */
@Service
@Transactional(readOnly = true)
public class UserQueryService {

}
