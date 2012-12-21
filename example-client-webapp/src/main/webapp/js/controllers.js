function PianoController($scope, $http) {
    $scope.NOTE_ON = 144;
    $scope.NOTE_OFF = 128;
    $scope.startOctave = 2;
    $scope.endOctave = 6;
    $scope.latency = 0;
    $scope.showNames = true;
    $scope.channel = 0;
    $scope.velocity = 60;

    $scope.sendMidiCommand = function (command, midiNote) {
        var midiMessage = {
            command: command,
            channel: $scope.channel,
            note: midiNote,
            velocity: $scope.velocity
        };
        var timeStart = new Date().getTime();
        $http.post("/data/midi/send", midiMessage).success(function () {
            $scope.latency = new Date().getTime() - timeStart;
        });
        if (command == $scope.NOTE_ON) {
            window.setTimeout(function () {
                $scope.sendMidiCommand($scope.NOTE_OFF, midiNote);
            }, 1000);
        }
    };

    $scope.resetKeyboard = function () {
        if ($scope.startOctave < 0 || $scope.endOctave > 8 || $scope.startOctave >= $scope.endOctave) {
            throw "Bad arguments";
        }
        var notes = [
            ["C", 0, "white"],
            ["Db", 0.5, "black"],
            ["D", 1, "white"],
            ["Eb", 1.5, "black"],
            ["E", 2, "white"],
            ["F", 3, "white"],
            ["Gb", 3.5, "black"],
            ["G", 4, "white"],
            ["Ab", 4.5, "black"],
            ["A", 5, "white"],
            ["Bb", 5.5, "black"],
            ["B", 6, "white"]
        ];
        var fullOctaves = $scope.endOctave - $scope.startOctave - 1;
        var totalWhites = 2 + fullOctaves * 7 + 1; // (A..B) + n*(C..B) + C

        function genOctave(oct, start, end) {
            var keys = [];
            for (var i = start; i < end; i++) {
                keys.push({
                    name: notes[i][0] + oct,
                    color: notes[i][2],
                    midiNote: oct * 12 + i + 12,
                    cssPosition: {
                        left: (100 * (notes[i][1] - 5 + (oct - $scope.startOctave) * 7) / totalWhites) + "%",
                        width: (100 / totalWhites) + "%"
                    }
                });
            }
            return keys;
        }

        var keys = genOctave($scope.startOctave, 9, 12); // A, Bb, B
        for (var oct = $scope.startOctave + 1; oct < $scope.endOctave; oct++) {
            keys = keys.concat(genOctave(oct, 0, 12));    // Full octave
        }
        $scope.keyboard = keys.concat(genOctave($scope.endOctave, 0, 1)); // C
    };

    $scope.resetKeyboard();
}
