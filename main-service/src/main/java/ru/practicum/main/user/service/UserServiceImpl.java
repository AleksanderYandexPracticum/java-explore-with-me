package ru.practicum.main.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.DuplicateEmailException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.UserMapper;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public List<UserDto> getUsersAdmin(List<Long> ids, Integer from, Integer size) {
        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        if (ids == null || ids.size() == 0) {
            return userRepository.findAll(pageable).stream()
                    .map((user) -> UserMapper.toUserDto(user))
                    .collect(Collectors.toList());
        } else {
            return userRepository.getUsersByIdIn(ids, pageable).stream()
                    .map((user) -> UserMapper.toUserDto(user))
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    public UserDto addUserAdmin(NewUserRequest newUserRequest) {
        User user = UserMapper.toUser(newUserRequest);
        UserDto userDto;
        try {
            userDto = UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate of the user's email address");
            throw new DuplicateEmailException(e.getMessage());
        }
        return userDto;
    }

    @Transactional
    @Override
    public void deleteUserAdmin(Long userId) {
        if (userRepository.getUserById(userId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        userRepository.removeUserById(userId);
    }

}