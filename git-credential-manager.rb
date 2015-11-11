class GitCredentialManager < Formula
  desc "Stores credentials for Git on Visual Studio Online (VSO)"
  homepage "http://java.visualstudio.com/Docs/tools/intro"
  url "https://github.com/Microsoft/Git-Credential-Manager-for-Mac-and-Linux/releases/download/git-credential-manager-${version}/git-credential-manager-${version}.jar"
  sha256 "TBD"

  bottle :unneeded

  depends_on :java => "1.7+"

  def install
    libexec.install "git-credential-manager-#{version}.jar"
    (bin/"git-credential-manager").write <<-EOS.undent
      #!/bin/sh
      java -jar "#{libexec}/git-credential-manager-#{version}.jar" "$@"
    EOS
  end

  test do
    system "java", "-jar", "#{libexec}/git-credential-manager-#{version}.jar", "version"
  end
end
