package com.oneday.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 클래스 정보 엔티티
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Entity
@Table(name = "classes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"teacher", "category"})
public class Classes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "class_id")
	private Integer classId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacher_id", nullable = false)
	private User teacher;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Categories category;

	@Column(name = "class_name", length = 50)
	private String className;

	@Column(name = "class_detail", length = 255)
	private String classDetail;

	@Column(name = "curriculum", length = 255)
	private String curriculum;

	@Column(name = "included", length = 255)
	private String included;

	@Column(name = "required", length = 255)
	private String required;

	@Column(name = "longitude", length = 20)
	private String longitude;

	@Column(name = "latitude", length = 20)
	private String latitude;

	@Column(name = "location", length = 255)
	private String location;

	@Column(name = "max_capacity")
	private Integer maxCapacity;

	@Column(name = "price")
	private Integer price;
}
