package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneday.core.entity.Classes;
import com.oneday.core.entity.User;

/**
 * Classes 엔티티 Repository
 * <p>
 * 클래스 정보에 대한 데이터 접근 계층입니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Repository
public interface ClassesRepository extends JpaRepository<Classes, Integer> {

	/**
	 * 특정 강사의 모든 클래스 조회
	 *
	 * @param teacher 강사 정보
	 * @return 강사의 클래스 목록
	 */
	List<Classes> findByTeacher(User teacher);
}
