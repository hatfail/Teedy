'use strict';

/**
 * 用户注册审核控制器
 */
angular.module('docs').controller('RegisterRequestReviewCtrl', function($scope, Restangular) {
  $scope.registerRequests = [];

  // 加载待审核注册请求
  $scope.loadRequests = function() {
    Restangular.one('user/register_requests').get().then(function(data) {
      $scope.registerRequests = data.requests;
    });
  };

  // 单条同意
  $scope.approve = function(req) {
    Restangular.one('user/register_request/' + req.id + '/approve').post().then(function() {
      $scope.loadRequests();
    });
  };

  // 单条拒绝
  $scope.reject = function(req) {
    Restangular.one('user/register_request/' + req.id + '/reject').post().then(function() {
      $scope.loadRequests();
    });
  };

  $scope.loadRequests();
});