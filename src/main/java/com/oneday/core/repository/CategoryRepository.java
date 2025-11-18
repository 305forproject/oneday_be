package com.oneday.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneday.core.entity.Categories;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {

}
