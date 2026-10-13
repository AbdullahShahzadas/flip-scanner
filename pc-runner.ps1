<#
.SYNOPSIS
  Flip Scanner PC Runner — lets Muse run terminal commands on this Windows PC.

  How it works:
  1. You start this script (one command, see SETUP note from Muse).
  2. Every 20 seconds it checks your private GitHub repo for a new command.
  3. It runs the command, saves the output, and pushes the result back.
  4. Muse reads the result from GitHub.

  You are in control: close this window any time to stop it.
  Every command and result is visible in the repo, nothing is hidden.
#>
param(
  [string]$RepoUrl = "",
  [int]$PollSeconds = 20
)

$RunnerDir = Join-Path $HOME "flip-scanner-runner"
$InboxRel   = "pc-inbox"
$StateFile  = Join-Path $RunnerDir ".runner-last-id.txt"

function Log([string]$msg) {
  Write-Host ("[{0}] {1}" -f (Get-Date -Format "HH:mm:ss"), $msg)
}

# --- first run: get the repo URL and clone it ---
if ([string]::IsNullOrWhiteSpace($RepoUrl)) {
  $RepoUrl = (Read-Host "Paste your GitHub repo URL").Trim()
}
$RepoUrl = $RepoUrl.Trim()
if (-not $RepoUrl.EndsWith(".git")) { $RepoUrl += ".git" }

if (-not (Test-Path (Join-Path $RunnerDir ".git"))) {
  Log "First run: downloading..."
  if (Test-Path $RunnerDir) { Remove-Item -Recurse -Force $RunnerDir }
  git clone $RepoUrl $RunnerDir 2>&1 | Out-Null
  if (-not (Test-Path (Join-Path $RunnerDir ".git"))) {
    Log "ERROR: could not clone. Check the URL and that GitHub login works (open GitHub in your browser once)."
    Read-Host "Press Enter to close"
    exit 1
  }
}

Set-Location $RunnerDir
$lastId = ""
if (Test-Path $StateFile) { $lastId = (Get-Content $StateFile -Raw).Trim() }

Log "Ready. Watching for commands - close this window any time to stop me."
while ($true) {
  try {
    git pull --ff-only --quiet 2>$null
    $cmdPath = Join-Path $InboxRel "command.json"

    if (Test-Path $cmdPath) {
      $cmd = (Get-Content $cmdPath -Raw) | ConvertFrom-Json
      $id  = [string]$cmd.id

      if ($id -ne "" -and $id -ne $lastId) {

        if ([string]$cmd.command -eq "EXIT") {
          Log "Got EXIT - stopping."
          Set-Content $StateFile $id
          break
        }

        Log "Running command: $id"
        $workdir = [string]$cmd.workdir
        if ([string]::IsNullOrWhiteSpace($workdir)) { $workdir = $RunnerDir }

        # v2: run in-process (simpler and more reliable than a child process).
        # NOTE: no hard timeout in v2 - keep sent commands quick and safe.
        $out  = ""
        $code = 0
        try {
          Set-Location -LiteralPath $workdir
          $out = Invoke-Expression ([string]$cmd.command) 2>&1 | Out-String
          if (-not $?) { $code = 1 }
        } catch {
          $code = 1
          $out  = "RUNNER ERROR: $($_.Exception.Message)"
        }
        if ($null -eq $out) { $out = "" }

        $result = [ordered]@{
          id          = $id
          exit_code   = $code
          output      = $out
          finished_at = (Get-Date).ToUniversalTime().ToString("o")
        }
        $resRel  = Join-Path $InboxRel ("result-{0}.json" -f $id)
        ($result | ConvertTo-Json -Depth 5) | Set-Content (Join-Path $RunnerDir $resRel) -Encoding UTF8

        git add $resRel 2>$null
        git -c user.name="pc-runner" -c user.email="pc-runner@local" commit -m "result $id" --quiet 2>$null
        git push --quiet 2>$null

        $lastId = $id
        Set-Content $StateFile $lastId
        Log "Result for $id sent back."
      }
    }
  } catch {
    Log ("Loop error: " + $_.Exception.Message)
  }
  Start-Sleep -Seconds $PollSeconds
}
Log "Runner stopped."
