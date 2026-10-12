# Windows desktop speech recognition, runs locally; voice activation requires no hotkey.
# Recognition language comes from installed Windows desktop speech recognizers.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Speech
$recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine
$recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar))
$recognizer.SetInputToDefaultAudioDevice()
Register-ObjectEvent -InputObject $recognizer -EventName SpeechRecognized -Action {
    $result = $Event.SourceEventArgs.Result
    if ($result.Confidence -ge 0.64) {
        [Console]::Out.WriteLine('RHYME:' + $result.Text)
        [Console]::Out.Flush()
    }
} | Out-Null
$recognizer.RecognizeAsync([System.Speech.Recognition.RecognizeMode]::Multiple)
while ($true) { Start-Sleep -Seconds 1 }
