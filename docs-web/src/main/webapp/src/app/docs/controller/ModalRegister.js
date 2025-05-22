'use strict';

/**
 * Modal register controller.
 */
angular.module('docs').controller('ModalRegister', function ($scope, $uibModalInstance, Restangular, $translate) {
  $scope.register = {
    storage_quota: 10000  // 默认值
  };
  $scope.registerError = '';

  $scope.submitRegister = function() {
    if (!$scope.register.username || !$scope.register.password || !$scope.register.passwordConfirm || !$scope.register.email) {
      $scope.registerError = $translate.instant('register.all_required');
      return;
    }
    if ($scope.register.password !== $scope.register.passwordConfirm) {
      $scope.registerError = $translate.instant('register.password_mismatch');
      return;
    }
    if ($scope.register.password.length < 8) {
      $scope.registerError = $translate.instant('register.password_too_short');
      return;
    }
    
    // 验证邮箱格式
    var emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test($scope.register.email)) {
      $scope.registerError = $translate.instant('validation.email');
      return;
    }
    
    Restangular.one('user').post('register_request', {
      username: $scope.register.username,
      password: $scope.register.password,
      email: $scope.register.email,
      storage_quota: $scope.register.storage_quota || 10000
    }).then(function() {
      $uibModalInstance.close();
    }, function(response) {
      $scope.registerError = response.data && response.data.message ? response.data.message : $translate.instant('register.unknown_error');
    });
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };
});