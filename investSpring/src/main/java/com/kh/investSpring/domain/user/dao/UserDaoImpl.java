package com.kh.investSpring.domain.user.dao;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
	
	private final SqlSessionTemplate session;
	
	@Override
	public int selectByUserId(String userId) {
		// TODO Auto-generated method stub
		return session.selectOne("user.selectByUserId", userId);
	}

	@Override
	public int countActiveUserByEmail(String email) {
		return session.selectOne("user.countActiveUserByEmail", email);
	}

	@Override
	public User selectUserByUserNo(long userNo) {
		return session.selectOne("user.selectUserByUserNo", userNo);
	}

	@Override
	public int insertUser(User user) {
		// TODO Auto-generated method stub
		return session.insert("user.insertUser", user);
	}

	@Override
	public int insertLocalUser(LocalUser localUser) {
		// TODO Auto-generated method stub
		return session.insert("user.insertLocalUser", localUser);
	}

	@Override
	public LocalUser selectLocalUserByUserId(String userId) {
		// TODO Auto-generated method stub
		return session.selectOne("user.selectLocalUserByUserId", userId);
	}

	@Override
	public int updateUserStatusDelete(Long userNo) {
		// TODO Auto-generated method stub
		return session.update("user.updateUserStatusDelete", userNo);
	}

	@Override
	public LocalUser selectLocalUserByUserNo(Long userNo) {
	    return session.selectOne("user.selectLocalUserByUserNo", userNo);
	}

	@Override
	public int updateUserInfo(User user) {
		// TODO Auto-generated method stub
		return session.update("user.updateUserInfo", user);
	}

    @Override
    public LocalUser selectLocalUserByUserIdAndUserName(
            String userId,
            String userName
    ) {

        LocalUser localUser = new LocalUser();
        localUser.setUserId(userId);
        localUser.setUserName(userName);

        return session.selectOne(
                "user.selectLocalUserByUserIdAndUserName",
                localUser
        );
    }
    
    @Override
    public int updatePassword(LocalUser localUser) {

        return session.update(
                "user.updatePassword",
                localUser
        );
    }

	@Override
	public void deleteInvestmentType(Long userNo) {
		session.delete("user.deleteInvestmentType", userNo);
	}

	@Override
	public int insertInvestmentType(Long userNo, int calculatedTotalPoint, String resultFile) {
	    Map<String, Object> param = new HashMap<>();
	    param.put("userNo", userNo);
	    param.put("totalPoint", calculatedTotalPoint);
	    param.put("resultFile", resultFile);

	    return session.insert("user.insertInvestmentType", param);
	}

	@Override
	public Integer selectInvestmentTotalPointByUserNo(Long userNo) {
		// TODO Auto-generated method stub
		return session.selectOne("user.selectInvestmentTotalPointByUserNo", userNo);
	}
	
}
