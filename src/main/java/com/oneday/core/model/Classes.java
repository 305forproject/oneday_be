package com.oneday.core.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "class_id")
	private Integer classId;

	@Column(name = "teacher_id")
	private Integer teacherId;

	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "class_name")
	private String className;

	@Column(name = "class_detail")
	private String classDetail;

	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Column(name = "end_at")
	private LocalDateTime endAt;

	@Column(name = "longitude")
	private String longitude;

	@Column(name = "latitude")
	private String latitude;

	@Column(name = "location")
	private String location;

	@Column(name = "zipcode")
	private String zipcode;

	@Column(name = "max_capacity")
	private int maxCapacity;

	@Column(name = "price")
	private int price;
}