package com.oneday.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.oneday.core.entity.Categories;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {

}
