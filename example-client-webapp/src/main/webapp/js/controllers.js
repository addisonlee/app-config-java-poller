function PollerController($scope, $http) {
    $scope.md5RealValue = "";
    $scope.md5StubValue = "";

    $scope.md5Real = function () {
        $http.get("/data/poller/md5").success(function (data) {
            $scope.md5RealValue = data;
        });
    };

    $scope.md5Stub = function () {
        $http.get("/data/poller/stub/md5").success(function (data) {
            $scope.md5StubValue = data;
        });
    };
}
