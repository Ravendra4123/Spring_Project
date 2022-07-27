package com.example.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.project.entity.User;

public interface UserRepository extends JpaRepository<User,Integer>{
	
    @Query("SELECT u FROM User u WHERE u.email = :email")
	User getUserByUserName(@Param("email")String email);
	
}
