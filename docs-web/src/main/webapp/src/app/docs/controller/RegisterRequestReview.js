app.controller('RegisterRequestReviewCtrl', function($scope, $http) {
  $scope.registerRequests = [];

  function loadRequests() {
    $http.get('/api/user/register_requests').then(function(res) {
      $scope.registerRequests = res.data.requests; // 只取数组部分
    });
  }

  $scope.approve = function(req) {
    $http.post('/api/user/register_request/' + req.id + '/approve').then(loadRequests);
  };

  $scope.reject = function(req) {
    $http.post('/api/user/register_request/' + req.id + '/reject').then(loadRequests);
  };

  loadRequests();
});