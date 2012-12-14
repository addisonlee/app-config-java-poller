function PianoController($scope, $http) {
    $scope.md5Value = "";

    $scope.md5 = function () {
        $http.get("/data/poller/md5").success(function (data) {
            $scope.md5Value = data;
        });
    };
}
