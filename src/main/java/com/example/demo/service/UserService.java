package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUser(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, User updatedUser) {
        User existingUser = getUser(id);
        if (existingUser == null) {
            return null;
        }
        updatedUser.setId(id);
        return userRepository.save(updatedUser);
    }

    public List<User> searchUsers(String searchString) {
        Pageable pageable = PageRequest.of(0, 10);
        return userRepository.searchByDisplayNameOrEmployeeId(searchString, pageable);
    }

    public User findEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId).orElse(null);
    }
}
