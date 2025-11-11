package com.oneday.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.oneday.core.entity.Classes;
import com.oneday.core.repository.ClassRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassService {

	private final ClassRepository classRepository;

	public Classes getClassById(int classId) {
		return classRepository.findById(classId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
	}

	public List<Classes> getAllClasses() {
		return classRepository.findAll();
	}
}
