'use strict';

angular.module('docs').controller('RegisterRequestList', function ($scope, Restangular, $translate, $window) {
  $scope.requests = [];
  $scope.selected = {};

  // 加载待审核注册请求
  $scope.loadRequests = function () {
    Restangular.one('user/register_requests').get().then(function (data) {
      $scope.requests = data.requests;
      $scope.selected = {};
    });
  };

  // 单条同意
  $scope.approve = function (req) {
    Restangular.one('user/register_request/' + req.id + '/approve').post().then(function () {
      $scope.loadRequests();
    });
  };

  // 单条拒绝
  $scope.reject = function (req) {
    var reason = $window.prompt($translate.instant('register.reason'), '');
    if (reason === null) return;
    Restangular.one('user/register_request/' + req.id + '/reject').post({ reason: reason }).then(function () {
      $scope.loadRequests();
    });
  };

  // 批量同意
  $scope.batchApprove = function () {
    var selectedIds = Object.keys($scope.selected).filter(function (id) { return $scope.selected[id]; });
    selectedIds.forEach(function (id) {
      Restangular.one('user/register_request/' + id + '/approve').post();
    });
    $scope.loadRequests();
  };

  // 批量拒绝
  $scope.batchReject = function () {
    var reason = $window.prompt($translate.instant('register.reason'), '');
    if (reason === null) return;
    var selectedIds = Object.keys($scope.selected).filter(function (id) { return $scope.selected[id]; });
    selectedIds.forEach(function (id) {
      Restangular.one('user/register_request/' + id + '/reject').post({ reason: reason });
    });
    $scope.loadRequests();
  };

  $scope.loadRequests();
});