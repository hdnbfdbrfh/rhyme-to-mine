# Rhyme to Mine - Windows offline speech recognition bridge
# Recognition starts automatically. No push-to-talk and no API key.
$ErrorActionPreference = 'Stop'
try {
    Add-Type -AssemblyName System.Speech
    $installed = [System.Speech.Recognition.SpeechRecognitionEngine]::InstalledRecognizers()
    if ($installed.Count -eq 0) { throw 'No Windows desktop speech recognizer installed. Add an English speech language in Windows.' }
    $english = $installed | Where-Object { $_.Culture.Name -like 'en-*' } | Select-Object -First 1
    if ($null -eq $english) { $english = $installed | Select-Object -First 1 }
    $recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine($english)
    $recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar))
    $recognizer.SetInputToDefaultAudioDevice()
    [Console]::Out.WriteLine('STATUS:Listening with ' + $english.Culture.Name)
    [Console]::Out.Flush()
    while ($true) {
        # Synchronous recognition avoids the PowerShell event-job output issue.
        $result = $recognizer.Recognize([TimeSpan]::FromSeconds(5))
        if ($null -ne $result -and $result.Confidence -ge 0.50 -and -not [string]::IsNullOrWhiteSpace($result.Text)) {
            [Console]::Out.WriteLine('RHYME:' + $result.Text)
            [Console]::Out.Flush()
        }
    }
} catch {
    [Console]::Out.WriteLine('ERROR:' + $_.Exception.Message)
    [Console]::Out.Flush()
    exit 1
} finally {
    if ($null -ne $recognizer) { $recognizer.Dispose() }
}
