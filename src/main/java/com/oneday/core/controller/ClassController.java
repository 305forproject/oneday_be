package com.oneday.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.oneday.core.entity.Classes;
import com.oneday.core.service.ClassService;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

	private final ClassService classService;

	@Autowired
	public ClassController(ClassService classService) {
		this.classService = classService;
	}

	@GetMapping
	public List<Classes> getAllClasses() {
		return classService.getAllClasses();
	}

	@GetMapping("/{classId}")
	public Classes getClassById(@PathVariable int classId) {
		return classService.getClassById(classId);
	}
}
